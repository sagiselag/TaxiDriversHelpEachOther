package com.android2.taxidrivershelpeachother.controller;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.android2.taxidrivershelpeachother.R;
import com.android2.taxidrivershelpeachother.model.FireBaseHandler;
import com.android2.taxidrivershelpeachother.model.Passenger;
import com.android2.taxidrivershelpeachother.model.ShuttleItem;
import com.android2.taxidrivershelpeachother.view.IRefreshableFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class ShuttleItemAdapter extends RecyclerView.Adapter<ShuttleItemAdapter.ItemViewHolder> {
    private List<ShuttleItem> items;
    private MyItemListener listener;
    private Context context;
    int MAX_SHUTTLE_DISTANCE = 0, MAX_SHUTTLE_TIME = 0, MIN_SHUTTLE_PRICE = 0;
    private String fragmentName;
    private boolean isInEditMode = false;
    private ShuttleLogic shuttleLogic;

    public Fragment getFragment() {
        return fragment;
    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }

    private Fragment fragment;

    public interface MyItemListener {
        void onItemClicked(int position, View view);
    }

    public ShuttleItemAdapter(List<ShuttleItem> items, String fragmentName) {
        this.items = items;
        this.fragmentName = fragmentName;
    }

    public void setListener(MyItemListener listener) {
        this.listener = listener;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        private MaterialAutoCompleteTextView originAddressTV, destinationAddressTV;
        private TextInputEditText priceTV, commissionFeeTV, remarksTV, shuttleDistanceTV, shuttleDateTV, shuttleTimeTV;
        private TextInputEditText supplierOrDriverNameTV, supplierOrDriverPhoneTV, clientNameTV, clientPhoneTV, shuttleStatusTV;
        private MaterialButton operationBtn, editBtn;
        private TextInputLayout shuttleStatusTIL;
        private CheckBox checkBox;
        private LinearLayout supplierLinearLayout;
        List<TextInputEditText> textViews = new ArrayList<>();

        public void setSupplierOrDriverNameTV(String supplierName) {
            supplierOrDriverNameTV.setText(supplierName);
        }

        public void setSupplierOrDriverPhoneTV(String supplierPhone) {
            supplierOrDriverPhoneTV.setText(supplierPhone);
        }

        public ItemViewHolder(@NonNull final View itemView) {
            super(itemView);

            this.originAddressTV = itemView.findViewById(R.id.SIC_from);
            this.destinationAddressTV = itemView.findViewById(R.id.SIC_destination);
            this.remarksTV = itemView.findViewById(R.id.SIC_remarks);
            this.shuttleDistanceTV = itemView.findViewById(R.id.SIC_driving_time_to_pickup_place);
            this.shuttleTimeTV = itemView.findViewById(R.id.SIC_time);
            this.shuttleDateTV = itemView.findViewById(R.id.SIC_date);
            this.priceTV = itemView.findViewById(R.id.SIC_price);
            this.commissionFeeTV = itemView.findViewById(R.id.SIC_commission_fee);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null)
                        listener.onItemClicked(getAdapterPosition(), v);
                }
            });

            if (fragmentName.equalsIgnoreCase("availableShuttles")) {
                this.operationBtn = itemView.findViewById(R.id.SIC_take);
            } else if (fragmentName.equalsIgnoreCase("commitmentShuttles")) {
                this.operationBtn = itemView.findViewById(R.id.SIC_cancel);
                this.supplierOrDriverNameTV = itemView.findViewById(R.id.new_shuttle_supplier_name_et);
                this.supplierOrDriverPhoneTV = itemView.findViewById(R.id.new_shuttle_supplier_phone_et);
                this.clientNameTV = itemView.findViewById(R.id.new_shuttle_client_name_et);
                this.clientPhoneTV = itemView.findViewById(R.id.new_shuttle_client_phone_et);
            } else if (fragmentName.equalsIgnoreCase("leadManagement")) {
                this.operationBtn = itemView.findViewById(R.id.SIC_cancel);
                this.editBtn = itemView.findViewById(R.id.SIC_edit);
                this.supplierOrDriverNameTV = itemView.findViewById(R.id.new_shuttle_supplier_name_et);
                this.supplierOrDriverPhoneTV = itemView.findViewById(R.id.new_shuttle_supplier_phone_et);
                this.clientNameTV = itemView.findViewById(R.id.new_shuttle_client_name_et);
                this.clientPhoneTV = itemView.findViewById(R.id.new_shuttle_client_phone_et);
                this.shuttleStatusTV = itemView.findViewById(R.id.shuttle_sell_status_ET);
                this.shuttleStatusTIL = itemView.findViewById(R.id.shuttle_sell_status_TIL);
                this.checkBox = itemView.findViewById(R.id.fixedPriceCheckBox);
                this.supplierLinearLayout = itemView.findViewById(R.id.supplier_info_and_title_layout);
                textViews.add(priceTV);
                textViews.add(commissionFeeTV);
                textViews.add(remarksTV);
                textViews.add(shuttleTimeTV);
                textViews.add(shuttleDateTV);
                textViews.add(clientNameTV);
                textViews.add(clientPhoneTV);
            }
        }
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // if we have few different types of ItemViewHolders we will have to check which layout need to be inflate below
        View view = null;
        ItemViewHolder itemViewHolder = null;

        if (fragmentName.equalsIgnoreCase("availableShuttles")) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shuttle_item_cell, parent, false);
        } else if (fragmentName.equalsIgnoreCase("commitmentShuttles")) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.taken_shuttle_cell, parent, false);
        } else if (fragmentName.equalsIgnoreCase("leadManagement")) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.for_sell_shuttle_cell, parent, false);
            shuttleLogic = new ShuttleLogic(parent.getContext(), null, view);
            shuttleLogic.setFragment(fragment);

        }

        // if we have few different types of ItemViewHolders we will have to check which one need to be create below
        if (view != null) {
            itemViewHolder = new ItemViewHolder(view);
        }
        context = parent.getContext();

        return itemViewHolder;
    }

    private void loadShuttleInfo(@NonNull ItemViewHolder holder, ShuttleItem item) {
        final String originAddress, destinationAddress, remarks;
        int shuttleDistanceTime, price, commissionFee, hours, minutes;
        String shuttleDistanceTimeStr = "", shuttleDistanceKmStr = "";
        String shuttleTime, shuttleDate;
        originAddress = item.getOriginAddress();
        destinationAddress = item.getDestinationAddress();
        remarks = item.getRemarks();
        shuttleDistanceTime = item.getDistanceToShuttleInMinutes();
        shuttleDate = item.getShuttleDate();
        shuttleTime = item.getShuttleTime();
        price = item.getPrice();
        commissionFee = item.getCommissionFee();
        hours = shuttleDistanceTime / 100;
        minutes = shuttleDistanceTime % 100;
        if (hours >= 1) {
            if (hours > 1) {
                shuttleDistanceTimeStr = String.valueOf(hours) + " " + context.getString(R.string.hours) + " ";
            } else {
                shuttleDistanceTimeStr = context.getString(R.string.hour) + " ";
            }
        }

        if (minutes == 1) {
            shuttleDistanceTimeStr += context.getString(R.string.minute);
        } else {
            shuttleDistanceTimeStr += String.valueOf(minutes) + " " + context.getString(R.string.minutes);
        }

        if (originAddress != null && !originAddress.equalsIgnoreCase("null"))
            holder.originAddressTV.setText(originAddress);
        else holder.originAddressTV.setText("");
        if (destinationAddress != null && !destinationAddress.equalsIgnoreCase("null"))
            holder.destinationAddressTV.setText(destinationAddress);
        else holder.destinationAddressTV.setText("");
        if (remarks != null && !remarks.equalsIgnoreCase("null")) holder.remarksTV.setText(remarks);
        else holder.remarksTV.setText("");
        if (holder.shuttleDistanceTV != null) {
            if (shuttleDistanceTime >= 0) holder.shuttleDistanceTV.setText(shuttleDistanceTimeStr);
            else holder.shuttleDistanceTV.setText("");
        }
        if (price >= 0) holder.priceTV.setText(String.valueOf(price));
        else holder.priceTV.setText("");
        if (commissionFee >= 0) holder.commissionFeeTV.setText(String.valueOf(commissionFee));
        else holder.commissionFeeTV.setText("");
        if (shuttleDate.equalsIgnoreCase("today")) {
            shuttleDate = context.getString(R.string.today);
        }
        if (shuttleTime.equalsIgnoreCase("immediate")) {
            shuttleTime = context.getString(R.string.immediate);
        }
        holder.shuttleDateTV.setText(shuttleDate);
        holder.shuttleTimeTV.setText(shuttleTime);
    }

    private void availableShuttlesSet(@NonNull ItemViewHolder holder, ShuttleItem item) {
        final String id;
        id = item.getId();

        holder.operationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id.length() > 0) {
                    FireBaseHandler.getInstance().setFragment(fragment);
                    FireBaseHandler.getInstance().takeShuttle(id);
                }
            }
        });
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        final ShuttleItem item = items.get(position);
        loadShuttleInfo(holder, item);

        if (fragmentName.equalsIgnoreCase("availableShuttles")) {
            availableShuttlesSet(holder, item);
        } else if (fragmentName.equalsIgnoreCase("commitmentShuttles")) {
            commitmentShuttlesSet(holder, item);
        } else if (fragmentName.equalsIgnoreCase("leadManagement")) {
            leadManagementShuttlesSet(holder, item);
        }
    }

    private void leadManagementShuttlesSet(final ItemViewHolder holder, final ShuttleItem item) {
        final String id;
        final String clientName, clientPhone;

        clientName = item.getPassenger().getName();
        clientPhone = item.getPassenger().getPhone();

        FireBaseHandler.getInstance().getDriverNameAndPhoneFromDB(item.getId(), holder);

        if (clientName != null && !clientName.equalsIgnoreCase("null"))
            holder.clientNameTV.setText(clientName);
        if (clientPhone != null && !clientPhone.equalsIgnoreCase("null"))
            holder.clientPhoneTV.setText(clientPhone);
        if (item.getHandlingDriverPhone() != null && item.getHandlingDriverPhone().length() > 0) {
            holder.shuttleStatusTV.setText(context.getString(R.string.sold));
            holder.supplierLinearLayout.setVisibility(View.VISIBLE);
//            holder.supplierOrDriverNameTV.setVisibility(View.VISIBLE);
//            holder.supplierOrDriverPhoneTV.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
//            holder.supplierOrDriverNameTV.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        } else {
            holder.shuttleStatusTV.setText(context.getString(R.string.still_looking_for_driver));
            holder.supplierLinearLayout.setVisibility(View.INVISIBLE);
            ViewGroup.LayoutParams params = holder.supplierLinearLayout.getLayoutParams();
            // Changes the height and width to the specified *pixels*
            params.height = 0;
            params.width = 100;
            holder.supplierLinearLayout.setLayoutParams(params);
//            holder.supplierOrDriverNameTV.setVisibility(View.INVISIBLE);
//            holder.supplierOrDriverPhoneTV.setHeight(0);
//            holder.supplierOrDriverNameTV.setHeight(0);
        }

        id = item.getId();
        holder.operationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doCancelButtonsOperation();
            }

            private void doCancelButtonsOperation() {
                if (!isInEditMode) {
                    if (id.length() > 0) {
                        FireBaseHandler.getInstance().setFragment(fragment);
                        FireBaseHandler.getInstance().deleteShuttleFromDB(id);
                        notifyDataSetChanged();
                    }
                } else {
                    loadShuttleInfo(holder, item);
                    holder.clientNameTV.setText(item.getPassenger().getName());
                    holder.clientPhoneTV.setText(item.getPassenger().getPhone());
                    changeEditBtnTextAndIcon(holder);
                    changeEditableStatus();
                }
            }

            private void changeEditableStatus() {
                for (TextInputEditText textView : holder.textViews) {
                    textView.setFocusableInTouchMode(!textView.isFocusableInTouchMode());
                }
            }
        });

        holder.editBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                changeEditBtnTextAndIcon(holder);
                changeEditableStatus();
                changeEditModeStatusAndDoEditButtonsOperation(holder);
            }

            private void changeEditableStatus() {
                for (TextInputEditText textView : holder.textViews) {
                    textView.setFocusableInTouchMode(!textView.isFocusableInTouchMode());
                }
            }

            private void changeEditModeStatusAndDoEditButtonsOperation(final ItemViewHolder holder) {
                if (!isInEditMode) {
                    // edit
                    changeEditBtnTextAndIcon(holder);
                } else {
                    // save
                    if (id.length() > 0) {
                        changeEditableStatus();
                        updateShuttleInfo();
                    }
                }
            }

            private void updateShuttleInfo() {
                String originAddress, destinationAddress, remarks, shuttleDate, shuttleTime, passengerName, passengerPhone;
                boolean isFixedPrice;
                int price = 0, commissionFee;
                Passenger passenger;
                ShuttleItem shuttleItem;

                shuttleTime = holder.shuttleTimeTV.getText().toString();
                shuttleDate = holder.shuttleDateTV.getText().toString();

                if (shuttleDate.equalsIgnoreCase(context.getString(R.string.today))) {
                    shuttleDate = LogicHandler.getTodayDateString();
                } else if (shuttleDate.equalsIgnoreCase(context.getString(R.string.tomorrow))) {
                    shuttleDate = LogicHandler.getTomorrowDateString();
                }

                if (shuttleTime.equalsIgnoreCase(context.getString(R.string.immediate))) {
                    shuttleTime = LogicHandler.getCurrentTimeString();
                }

                originAddress = holder.originAddressTV.getText().toString();
                destinationAddress = holder.destinationAddressTV.getText().toString();
                isFixedPrice = holder.checkBox.isChecked();
                if (isFixedPrice) {
                    String priceStr = holder.priceTV.getText().toString().replaceAll("[^0-9.]", "");
                    price = Integer.valueOf(priceStr);
                }

                commissionFee = Integer.valueOf(holder.commissionFeeTV.getText().toString().replaceAll("[^0-9.]", ""));
                remarks = holder.remarksTV.getText().toString();
                passengerName = holder.clientNameTV.getText().toString();
                passengerPhone = holder.clientPhoneTV.getText().toString();

                passenger = new Passenger(passengerName, passengerPhone);
                if (shuttleLogic.getOriginLatLng() != null) {
                    shuttleItem = new ShuttleItem(
                            shuttleLogic.getOriginLatLng(), originAddress, destinationAddress, remarks, shuttleDate, shuttleTime, price, commissionFee, passenger,
                            shuttleLogic.getShuttleDistanceInKm(), shuttleLogic.getShuttleDistanceInMinutes());
                } else {
                    shuttleItem = new ShuttleItem(item.getOriginLatLng(), originAddress, destinationAddress, remarks, shuttleDate, shuttleTime, price, commissionFee, passenger,
                            item.getShuttleDistanceInKm(), item.getShuttleDistanceInMinutes());
                }

                shuttleItem.setHandlingDriverPhone(item.getHandlingDriverPhone());
                shuttleItem.setPublishedBy(item.getPublishedBy());

                shuttleLogic.setShuttleID(id);
                FireBaseHandler.getInstance().setFragment(fragment);

                shuttleLogic.publishShuttle(shuttleItem);
            }
        });
    }


    private void commitmentShuttlesSet(ItemViewHolder holder, ShuttleItem item) {
        final String id;
        final String clientName, clientPhone, publishedBy, supplierPhone;

        clientName = item.getPassenger().getName();
        clientPhone = item.getPassenger().getPhone();
        publishedBy = item.getPublishedBy();

        int index = publishedBy.indexOf(")");
        supplierPhone = publishedBy.substring(index + 2);

        FireBaseHandler.getInstance().getSupplierOrDriverNameFromDB(publishedBy, holder);

        if (clientName != null && !clientName.equalsIgnoreCase("null"))
            holder.clientNameTV.setText(clientName);
        if (clientPhone != null && !clientPhone.equalsIgnoreCase("null"))
            holder.clientPhoneTV.setText(clientPhone);
        if (supplierPhone != null && !supplierPhone.equalsIgnoreCase("null"))
            holder.supplierOrDriverPhoneTV.setText(supplierPhone);

        id = item.getId();
        holder.operationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (id.length() > 0) {
                    FireBaseHandler.getInstance().setFragment(fragment);
                    FireBaseHandler.getInstance().cancelShuttle(id);
                }
            }
        });
    }

    private void changeEditBtnTextAndIcon(final ItemViewHolder holder) {
        isInEditMode = !isInEditMode;

        if (!isInEditMode) {
            // edit
            holder.editBtn.setText(context.getString(R.string.edit));
            holder.editBtn.setIconResource(android.R.drawable.ic_menu_edit);
            holder.operationBtn.setText(context.getString(R.string.delete));
            holder.operationBtn.setIconResource(android.R.drawable.ic_menu_delete);
        } else {
            // save
            holder.editBtn.setText(context.getString(R.string.save));
            holder.editBtn.setIconResource(android.R.drawable.ic_menu_save);
            holder.operationBtn.setText(context.getString(R.string.cancel_shuttle));
            holder.operationBtn.setIconResource(android.R.drawable.ic_menu_close_clear_cancel);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // don't need to be implement in this application
    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }
}
